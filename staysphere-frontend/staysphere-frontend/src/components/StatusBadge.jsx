export default function StatusBadge({ status }) {
  if (!status) return null
  const cls = `tag tag-${status.toLowerCase()}`
  return <span className={cls}>{status.replace(/_/g, ' ')}</span>
}
