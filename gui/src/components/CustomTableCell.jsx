import TableCell from "@mui/material/TableCell";
import StatusIcon from "./StatusIcon.jsx";
import dayjs from "dayjs";

export default function CustomTableCell({children, date, onClick, isBold, isHover, classNames, isSynced, isConflicted}) {
  return <TableCell
    onClick={onClick}
    className={`${classNames} w-fit border border-solid border-gray-200 ${isBold ? "font-bold" : ""} ${isHover ? "hover:bg-blue-50 cursor-pointer" : ""}`}
  >
    <div className="flex flex-col">
      <div className="flex items-center">
        {date && <div className="mr-1">{dayjs(date).format("DD.MM")}</div>}
        <StatusIcon isSynced={isSynced} isConflicted={isConflicted} />
      </div>
      {children}
    </div>
  </TableCell>
}