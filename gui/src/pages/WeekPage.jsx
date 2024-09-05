import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import dayjs from "dayjs";
import dateTimeService from "../service/dateTimeService.js";
import {startHourOfDay} from "../config/timeConfig.js";
import {useQuery} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {CircularProgress} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import CustomTableCell from "../components/CustomTableCell.jsx";
import useViewChanger from "../hooks/useViewChanger.js";

export default function WeekPage() {
  const offset = startHourOfDay;

  const {date, setDate, addAlert} = useAppContext();
  const {changeView} = useViewChanger();


  const {
    data,
    isPending,
    isPlaceholderData,
  } = useQuery({
    queryKey: [timeLogApi.key, "week", date, offset],
    queryFn: () => {
      return timeLogApi.getHoursForWeek({date: dateTimeService.getFormattedDate(date), offset});
    },
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Getting hours for week failed:", error);
    },
    placeholderData: (prev) => prev,
    retryDelay: 300,
  });

  const handleClick = (date) => {
    setDate(dayjs(date))
    changeView("Day")
  }

  const getTotalTimeForTicket = (ticket) => {
    const totalTime = data.reduce((result, {ticketDurations}) => {
      const ticketDuration = ticketDurations.find(td => td.ticket === ticket);
      result += dateTimeService.parseMinutes(ticketDuration.duration)
      return result;
    }, 0)
    return dateTimeService.formatDuration(totalTime);
  }

  if (isPending) {
    return (
      <div className="absolute inset-1/2">
        <CircularProgress />
      </div>
    );
  }


  return (
    <div>
      <TableContainer className="flex mx-auto my-6 w-2/3">
        <Table size="small">
          <TableHead>
            <TableRow>
              <CustomTableCell><></>
              </CustomTableCell>
              {data.map(dayInfo => (
                <CustomTableCell
                  key={dayInfo.date}
                  isHover
                  classNames={`${dayInfo.conflicted ? "bg-red-200" : ""} ${dayInfo.inProgress ? "bg-blue-200" : ""}`}
                  onClick={() => handleClick(dayInfo.date)}
                >
                  {dayInfo.dayName}
                </CustomTableCell>
              ))}
              <CustomTableCell isBold>Total</CustomTableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data[0]?.ticketDurations.map(({ticket}) => (
              <TableRow key={ticket}>
                <CustomTableCell isBold={ticket === "Total"}>{ticket}</CustomTableCell>
                {data.map(dayInfo => {
                  const ticketDuration = dayInfo.ticketDurations.find(td => td.ticket === ticket);
                  return (
                    <CustomTableCell
                      key={`${dayInfo.date}-${ticket}`}
                      isBold={ticket === "Total"}
                      isHover={ticket === "Total"}
                      onClick={() => {
                        if (ticket === "Total") {
                          handleClick(dayInfo.date);
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
      </TableContainer>
    </div>
  )
}