import TableCell from "@mui/material/TableCell";
import TimeLogStatusIcons from "./TimeLogStatusIcons.jsx";
import dayjs from "dayjs";

export default function CustomTableCell({children, date, onClick, isBold, isHover, classNames, syncStatus, isConflicted}) {
  return <TableCell
    onClick={onClick}
    className={`${classNames} w-fit text-nowrap border border-solid border-gray-200 ${isBold ? "font-bold" : ""} ${isHover ? "hover:bg-blue-50 cursor-pointer" : ""}`}
  >
    <div className="flex flex-col">
      <div className="flex items-center">
        {date && <div className="mr-1">{dayjs(date).format("DD.MM")}</div>}
        <TimeLogStatusIcons syncStatus={syncStatus} isConflicted={isConflicted} />
      </div>
      {children}
    </div>
  </TableCell>
}