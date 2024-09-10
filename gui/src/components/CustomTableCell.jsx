import TableCell from "@mui/material/TableCell";
import {Tooltip} from "@mui/material";
import WarningAmberIcon from "@mui/icons-material/WarningAmber.js";
import PendingIcon from "@mui/icons-material/Pending.js";

export default function CustomTableCell({children, onClick, isBold, isHover, classNames, isConflicted, isInProgress}) {
  return <TableCell
    onClick={onClick}
    className={`${classNames} border border-solid border-gray-200 ${isInProgress ? "bg-blue-200" : ""} ${isConflicted ? "bg-red-200" : ""} ${isBold ? "font-bold" : ""} ${isHover ? "hover:bg-blue-50 cursor-pointer" : ""}`}
  >
    <div className={`${isConflicted || isInProgress ? "flex" : ""}`}>
    {isConflicted && <Tooltip title="Conflicted"><WarningAmberIcon color="error" className="mr-1" /></Tooltip>}
    {isInProgress && <Tooltip title="In Progress"><PendingIcon color="primary" className="mr-1" /></Tooltip>}

    {children}
    </div>
  </TableCell>
}