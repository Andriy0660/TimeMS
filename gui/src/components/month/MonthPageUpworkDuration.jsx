export default function MonthPageUpworkDuration({duration}) {
  return (
    <div className="flex justify-center items-center w-full bg-transparent text-green-500 text-base font-medium ">
      {duration !== "0h 0m" ? duration : ""}
    </div>
  )
}