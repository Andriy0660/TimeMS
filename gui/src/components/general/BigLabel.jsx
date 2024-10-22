export default function BigLabel({children, className}) {
  return (
    <div className={`flex items-center w-fit p-2 text-4xl border border-solid rounded-xl text-blue-400 shadow-md ${className}`}>
      {children}
    </div>
  )
}