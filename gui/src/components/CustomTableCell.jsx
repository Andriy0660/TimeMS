import TableCell from "@mui/material/TableCell";

export default function CustomTableCell({children, onClick, isBold, isHover, classNames}) {
  return <TableCell
    onClick={onClick}
    className={`${classNames} border border-solid border-gray-200 ${isBold ? "font-bold" : ""} ${isHover ? "hover:bg-blue-50 cursor-pointer" : ""}`}
  >
    {children}
  </TableCell>
}