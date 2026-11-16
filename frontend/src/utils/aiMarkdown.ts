/**
 * 轻量安全的中文 Markdown 转 HTML 渲染器（用于 DeepSeek AI 赛后复盘报告展示）
 * 支持标题、加粗、行内代码、无序/有序列表、引用块、分割线与表格（兼容行间带空行的表格）
 */
const escapeHtml = (raw: string): string =>
  raw
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');

const formatInline = (text: string): string => {
  let out = escapeHtml(text);
  // 转换 **加粗**
  out = out.replace(/\*\*(.+?)\*\*/g, '<strong class="text-amber-300 font-black">$1</strong>');
  // 转换 `代码/数值高亮`
  out = out.replace(
    /`([^`]+)`/g,
    '<code class="px-1.5 py-0.5 rounded bg-white/10 text-cyan-300 font-mono text-xs">$1</code>'
  );
  return out;
};

const isTableRow = (line: string): boolean => line.startsWith('|') && line.endsWith('|') && line.length >= 3;

const isTableSeparator = (line: string): boolean => {
  if (!isTableRow(line)) return false;
  const cells = line
    .slice(1, -1)
    .split('|')
    .map((c) => c.trim());
  return cells.length > 0 && cells.every((c) => /^:?-{2,}:?$/.test(c));
};

const parseTableCells = (line: string): string[] =>
  line
    .slice(1, -1)
    .split('|')
    .map((c) => c.trim());

const parseAlignClass = (sepCell: string): string => {
  const trimmed = sepCell.trim();
  if (trimmed.startsWith(':') && trimmed.endsWith(':')) return 'text-center';
  if (trimmed.endsWith(':')) return 'text-right';
  return 'text-left';
};

/**
 * 将 AI 返回的 Markdown 文本渲染为安全的 HTML 字符串
 * @param markdown 原始 Markdown 文本
 * @param accentBulletClass 无序列表圆点高亮颜色类
 * @param h3ColorClass 三级标题高亮颜色类
 */
export const renderAiMarkdown = (
  markdown: string,
  accentBulletClass = 'text-rose-400',
  h3ColorClass = 'text-rose-300'
): string => {
  if (!markdown) return '';
  const lines = markdown.split(/\r?\n/);
  const htmlParts: string[] = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line) {
      htmlParts.push('<div class="h-1.5"></div>');
      continue;
    }

    // 优先识别 Markdown 表格（兼容表头与数据行之间夹杂空行的输出）
    if (isTableRow(line)) {
      let sepIdx = i + 1;
      while (sepIdx < lines.length && !lines[sepIdx].trim()) {
        sepIdx++;
      }
      if (sepIdx < lines.length && isTableSeparator(lines[sepIdx].trim())) {
        const headers = parseTableCells(line);
        const aligns = parseTableCells(lines[sepIdx].trim()).map(parseAlignClass);
        const bodyRows: string[][] = [];

        let j = sepIdx + 1;
        while (j < lines.length) {
          const nextLine = lines[j].trim();
          if (!nextLine) {
            // 若空行之后紧跟表格数据行，则继续归入同一张表；否则结束表格
            let peek = j + 1;
            while (peek < lines.length && !lines[peek].trim()) {
              peek++;
            }
            if (peek < lines.length && isTableRow(lines[peek].trim()) && !isTableSeparator(lines[peek].trim())) {
              j = peek;
              continue;
            }
            break;
          }
          if (!isTableRow(nextLine) || isTableSeparator(nextLine)) {
            break;
          }
          bodyRows.push(parseTableCells(nextLine));
          j++;
        }

        const thHtml = headers
          .map(
            (h, idx) =>
              `<th class="px-3 py-2.5 text-[11px] sm:text-xs font-black text-amber-300 bg-white/[0.04] border-b border-white/10 whitespace-nowrap ${aligns[idx] || 'text-left'}">${formatInline(h)}</th>`
          )
          .join('');

        const trHtml = bodyRows
          .map((row) => {
            const tds = headers
              .map((_, idx) => {
                const cell = row[idx] ?? '';
                return `<td class="px-3 py-2.5 text-xs text-slate-200 border-b border-white/[0.06] align-top ${aligns[idx] || 'text-left'}">${formatInline(cell)}</td>`;
              })
              .join('');
            return `<tr class="hover:bg-white/[0.03] transition-colors">${tds}</tr>`;
          })
          .join('');

        htmlParts.push(
          `<div class="overflow-x-auto my-3 rounded-xl border border-white/10 bg-[#060a15]/60"><table class="w-full border-collapse"><thead><tr>${thHtml}</tr></thead><tbody>${trHtml}</tbody></table></div>`
        );

        i = j - 1;
        continue;
      }
    }

    if (/^---+$/.test(line)) {
      htmlParts.push('<hr class="border-white/10 my-4" />');
      continue;
    }
    if (line.startsWith('> ')) {
      htmlParts.push(
        `<blockquote class="border-l-2 border-cyan-400/60 pl-3 py-1 my-1 bg-white/[0.02] rounded-r-lg text-slate-300 italic">${formatInline(line.slice(2))}</blockquote>`
      );
      continue;
    }
    if (line.startsWith('#### ')) {
      htmlParts.push(
        `<h5 class="text-xs sm:text-sm font-black text-cyan-300 mt-4 mb-1.5 flex items-center gap-1.5">${formatInline(line.slice(5))}</h5>`
      );
      continue;
    }
    if (line.startsWith('### ')) {
      htmlParts.push(
        `<h4 class="text-sm sm:text-base font-black ${h3ColorClass} mt-5 mb-2 pb-1.5 border-b border-white/10">${formatInline(line.slice(4))}</h4>`
      );
      continue;
    }
    if (line.startsWith('## ')) {
      htmlParts.push(
        `<h3 class="text-base sm:text-lg font-black text-amber-300 mt-5 mb-2.5 pb-2 border-b border-amber-400/25">${formatInline(line.slice(3))}</h3>`
      );
      continue;
    }
    if (line.startsWith('# ')) {
      htmlParts.push(
        `<h2 class="text-lg sm:text-xl font-black text-white mt-5 mb-3">${formatInline(line.slice(2))}</h2>`
      );
      continue;
    }
    if (/^[-*]\s+/.test(line)) {
      htmlParts.push(
        `<div class="flex items-start gap-2 pl-2 py-0.5"><span class="${accentBulletClass} font-black mt-0.5">•</span><div class="flex-1">${formatInline(line.replace(/^[-*]\s+/, ''))}</div></div>`
      );
      continue;
    }
    if (/^\d+\.\s+/.test(line)) {
      const match = line.match(/^(\d+\.)\s+(.*)$/);
      if (match) {
        htmlParts.push(
          `<div class="flex items-start gap-2 pl-1 py-1 bg-white/[0.02] rounded-lg px-2.5"><span class="text-amber-400 font-mono font-black shrink-0">${match[1]}</span><div class="flex-1">${formatInline(match[2])}</div></div>`
        );
        continue;
      }
    }
    htmlParts.push(`<p class="leading-relaxed text-slate-200">${formatInline(line)}</p>`);
  }

  return htmlParts.join('\n');
};
