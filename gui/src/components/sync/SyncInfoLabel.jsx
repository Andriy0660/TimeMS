export default function SyncInfoLabel({children, className, color}) {
  return (
    <div className={`px-2 rounded-md bg-${color}-500 text-white flex items-center ${className}`}>
      {children}
    </div>
  )
}