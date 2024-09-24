import TableCell from "@mui/material/TableCell";
import StatusIcon from "./StatusIcon.jsx";

export default function CustomTableCell({children, onClick, isBold, isHover, classNames, isSynced, isConflicted}) {
  return <TableCell
    onClick={onClick}
    className={`${classNames} border border-solid border-gray-200 ${isConflicted ? "bg-red-200" : ""} ${isBold ? "font-bold" : ""} ${isHover ? "hover:bg-blue-50 cursor-pointer" : ""}`}
  >
    <div className={`${isSynced || isConflicted ? "flex" : ""}`}>
      <StatusIcon isSynced={isSynced} isConflicted={isConflicted} />
      {children}
    </div>
  </TableCell>
}