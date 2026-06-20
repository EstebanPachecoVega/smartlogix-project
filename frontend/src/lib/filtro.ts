export const RANGES = [
  { value: '7', label: '7 días' },
  { value: '30', label: '30 días' },
  { value: '90', label: '3 meses' },
  { value: '180', label: '6 meses' },
  { value: '365', label: '12 meses' },
  { value: 'all', label: 'Todos los tiempos' },
] as const;

export type DiasFilter = (typeof RANGES)[number]['value'];

export function filterByDate<T>(
  items: T[],
  dateField: keyof T,
  dias: string,
): T[] {
  if (dias === 'all') return items;
  const numDias = parseInt(dias, 10);
  const cutoff = new Date();
  cutoff.setHours(0, 0, 0, 0);
  cutoff.setDate(cutoff.getDate() - numDias);
  return items.filter((item) => {
    const raw = item[dateField];
    if (typeof raw !== 'string' || !raw) return false;
    const d = new Date(raw.split('T')[0]);
    d.setHours(0, 0, 0, 0);
    return d >= cutoff;
  });
}
