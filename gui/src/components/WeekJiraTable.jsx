import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import CustomTableCell from "./CustomTableCell.jsx";
import TableBody from "@mui/material/TableBody";
import Table from "@mui/material/Table";
import dateTimeService from "../service/dateTimeService.js";

export default function WeekJiraTable({dayInfos, handleClickDate}) {

  const getTotalTimeForTicket = (ticket) => {
    const totalTime = dayInfos.reduce((result, {ticketDurations}) => {
      const ticketDuration = ticketDurations.find(td => td.ticket === ticket);
      result += dateTimeService.parseMinutes(ticketDuration.duration)
      return result;
    }, 0)
    return dateTimeService.formatDuration(totalTime);
  }

  return (
    <Table size="small">
      <TableHead>
        <TableRow>
          <CustomTableCell><></>
          </CustomTableCell>
          {dayInfos.map(dayInfo => (
            <CustomTableCell
              key={dayInfo.date}
              date={dayInfo.date}
              isHover
              jiraSyncStatus={dayInfo?.jiraSyncInfo.status}
              isConflicted={dayInfo.conflicted}
              onClick={() => handleClickDate(dayInfo.date)}
            >
              <div>{dayInfo.dayName}</div>
            </CustomTableCell>
          ))}
          <CustomTableCell isBold>Total</CustomTableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {dayInfos[0]?.ticketDurations.map(({ticket}) => (
          <TableRow key={ticket}>
            <CustomTableCell isBold={ticket === "Total"}>{ticket}</CustomTableCell>
            {dayInfos.map(dayInfo => {
              const ticketDuration = dayInfo.ticketDurations.find(td => td.ticket === ticket);
              return (
                <CustomTableCell
                  key={`${dayInfo.date}-${ticket}`}
                  isBold={ticket === "Total"}
                  isHover={ticket === "Total"}
                  onClick={() => {
                    if (ticket === "Total") {
                      handleClickDate(dayInfo.date);
                    }
                  }}
                >
                  {ticketDuration.duration !== "0h 0m" ? ticketDuration.duration : ""}
                </CustomTableCell>
              );
            })}
            <CustomTableCell isBold>{getTotalTimeForTicket(ticket)}</CustomTableCell>
          </TableRow>
        ))
        }
      </TableBody>
    </Table>
  )
}