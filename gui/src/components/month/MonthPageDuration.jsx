export default function MonthPageDuration({duration}) {
  return (
    <div className="flex justify-center items-center w-full bg-transparent text-black text-lg font-medium ">
      {duration !== "0h 0m" ? duration : ""}
    </div>
  )
}