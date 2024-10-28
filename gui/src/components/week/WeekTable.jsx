import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import CustomTableCell from "./CustomTableCell.jsx";
import TableBody from "@mui/material/TableBody";
import Table from "@mui/material/Table";
import {isUpworkSyncingEnabled, upworkTimeCf} from "../../config/config.js";
import SyncUpworkDuration from "../sync/SyncUpworkDuration.jsx";
import dateTimeService from "../../service/dateTimeService.js";

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
          {dayInfos.map(dayInfo => {
            const upworkDuration = dateTimeService.formatMinutesToHM(Math.round(
              dateTimeService.getMinutesFromHMFormat(dayInfo.duration) / upworkTimeCf));
            return (
              <CustomTableCell isBold key={dayInfo.date}>
                {dayInfo.duration}
                {isUpworkSyncingEnabled && (
                  <SyncUpworkDuration duration={upworkDuration} textSize="small" />
                )}
              </CustomTableCell>
            )
          })}
        </TableRow>
      </TableBody>
    </Table>
  )
}