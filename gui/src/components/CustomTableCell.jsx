import TableCell from "@mui/material/TableCell";
import StatusIcon from "./StatusIcon.jsx";

export default function CustomTableCell({children, onClick, isBold, isHover, classNames, isSynced, isConflicted, isInProgress}) {
  return <TableCell
    onClick={onClick}
    className={`${classNames} border border-solid border-gray-200 ${isInProgress ? "bg-blue-200" : ""} ${isConflicted ? "bg-red-200" : ""} ${isBold ? "font-bold" : ""} ${isHover ? "hover:bg-blue-50 cursor-pointer" : ""}`}
  >
    <div className={`${isSynced || isConflicted || isInProgress ? "flex" : ""}`}>
      <StatusIcon isSynced={isSynced} isConflicted={isConflicted} isInProgress={isInProgress} />
      {children}
    </div>
  </TableCell>
}