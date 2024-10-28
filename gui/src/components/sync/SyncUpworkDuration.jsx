export default function SyncUpworkDuration({duration, textSize}) {
  return (
    <div className={`flex justify-center items-center w-full bg-transparent text-green-500 text-${textSize} font-medium`}>
      {duration !== "0h 0m" ? duration : ""}
    </div>
  )
}