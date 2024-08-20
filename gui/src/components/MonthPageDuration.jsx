export default function MonthPageDuration({handleClickDate, title}) {
  return (
    <div
      onClick={handleClickDate}
      className="flex justify-center w-full bg-transparent text-black text-lg font-medium hover:bg-blue-50 hover:cursor-pointer"
    >
      {title !== "0h 0m" ? title : ""}
    </div>
  )
}