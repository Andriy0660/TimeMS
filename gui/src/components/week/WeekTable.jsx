import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import CustomTableCell from "./CustomTableCell.jsx";
import TableBody from "@mui/material/TableBody";
import Table from "@mui/material/Table";

export default function WeekTable({dayInfos, handleClickDate}) {
  return (
    <Table size="small" aria-label="a dense table">
      <TableHead>
        <TableRow>
          {dayInfos.map(dayInfo => <CustomTableCell
            key={dayInfo.date}
            date={dayInfo.date}
            isHover
            isConflicted={dayInfo.conflicted}
            onClick={() => handleClickDate(dayInfo.date)}
          >
            <div>{dayInfo.dayName}</div>
          </CustomTableCell>)}
        </TableRow>
      </TableHead>
      <TableBody>
        <TableRow>
          {dayInfos.map(dayInfo => <CustomTableCell isBold key={dayInfo.date}>{dayInfo.duration}</CustomTableCell>)}
        </TableRow>
      </TableBody>
    </Table>
  )
}