import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import CustomTableCell from "./CustomTableCell.jsx";
import TableBody from "@mui/material/TableBody";
import Table from "@mui/material/Table";
import {isExternalServiceSyncingEnabled, externalTimeLogTimeCf} from "../../config/config.js";
import SyncExternalTimeLogDuration from "../sync/SyncExternalTimeLogDuration.jsx";
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
            externalTimeLogSyncStatus={dayInfo.externalTimeLogSyncInfo.status}
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
            const externalTimeLogDuration = dateTimeService.formatMinutesToHM(Math.round(
              dateTimeService.getMinutesFromHMFormat(dayInfo.duration) / externalTimeLogTimeCf));
            return (
              <CustomTableCell isBold key={dayInfo.date}>
                {dayInfo.duration}
                {isExternalServiceSyncingEnabled && (
                  <SyncExternalTimeLogDuration duration={externalTimeLogDuration} textSize="small" />
                )}
              </CustomTableCell>
            )
          })}
        </TableRow>
      </TableBody>
    </Table>
  )
}