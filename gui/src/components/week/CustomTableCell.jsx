import TableCell from "@mui/material/TableCell";
import TimeLogStatusIcons from "../timeLog/TimeLogStatusIcons.jsx";
import dayjs from "dayjs";

export default function CustomTableCell({children, date, onClick, isBold, isHover, classNames, jiraSyncStatus, upworkSyncStatus, isConflicted}) {
  return <TableCell
    onClick={onClick}
    className={`${classNames} w-fit text-nowrap border border-solid border-gray-200 ${isBold ? "font-bold" : ""} ${isHover ? "hover:bg-blue-50 cursor-pointer" : ""}`}
  >
    <div className="flex flex-col justify-center items-center">
      {date && (
        <div className="flex items-center">
          <div className="mr-1">{dayjs(date).format("DD.MM")}</div>
          <TimeLogStatusIcons isConflicted={isConflicted} jiraSyncStatus={jiraSyncStatus} upworkSyncStatus={upworkSyncStatus} showOnlyNotSuccessfullySynced={true} />
        </div>
      )}
      {children}
    </div>
  </TableCell>
}