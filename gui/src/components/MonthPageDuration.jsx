export default function MonthPageDuration({title}) {
  return (
    <div className="flex justify-center items-center w-full bg-transparent text-black text-lg font-medium ">
      {title !== "0h 0m" ? title : ""}
    </div>
  )
}