import StatusIcon from "./StatusIcon.jsx";

export default function MonthPageDuration({handleClickDate, title, isSynced, isConflicted, isInProgress}) {
  return (
    <div
      onClick={handleClickDate}
      className="flex justify-center items-center w-full bg-transparent text-black text-lg font-medium hover:bg-blue-50 hover:cursor-pointer"
    >
      <StatusIcon isSynced={isSynced} isConflicted={isConflicted} isInProgress={isInProgress} />
      {title !== "0h 0m" ? title : ""}
    </div>
  )
}