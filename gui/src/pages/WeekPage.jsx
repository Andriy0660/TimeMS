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
import {CircularProgress, FormControlLabel, Switch} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import CustomTableCell from "../components/CustomTableCell.jsx";
import useViewChanger from "../hooks/useViewChanger.js";
import TimeLogList from "../components/TimeLogList.jsx";
import {useState} from "react";
import useTimeLogMutations from "../hooks/useTimeLogMutations.js";
import useProcessedTimeLogs from "../hooks/useProcessedTimeLogs.js";

export default function WeekPage() {
  const offset = startHourOfDay;

  const {changeView} = useViewChanger();
  const [isTableView, setIsTableView] = useState(true);
  const {date, setDate, addAlert, mode} = useAppContext();

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
    retryDelay: 300,
  });

  const timeLogMutations = useTimeLogMutations();

  const {
    groupByDescription, setGroupByDescription, timeLogs
  } = useProcessedTimeLogs();

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
      <div className="w-3/5 mx-auto flex items-center">
        <FormControlLabel
          control={
            <Switch
              checked={isTableView}
              onChange={(event) => setIsTableView((event.target.checked))}
            />
          }
          label="List"
          labelPlacement="start"
          className="mr-0.5"
        />
        <div>
          Table
        </div>
        {!isTableView && <FormControlLabel
          control={
            <Switch
              checked={groupByDescription}
              onChange={(event) => setGroupByDescription((event.target.checked))}
            />
          }
          label="Group"
          labelPlacement="start"
          className="ml-10"
        />
        }
      </div>
      {isTableView && <TableContainer className="flex mx-auto mb-3 w-fit">
        <Table size="small">
          <TableHead>
            <TableRow>
              <CustomTableCell><></>
              </CustomTableCell>
              {data.map(dayInfo => (
                <CustomTableCell
                  key={dayInfo.date}
                  isHover
                  isSynced={dayInfo.synced}
                  isConflicted={dayInfo.conflicted}
                  onClick={() => handleClick(dayInfo.date)}
                >
                  <div className="mr-1">{dayjs(dayInfo.date).format("DD.MM")}</div>
                  <div>{dayInfo.dayName}</div>
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
      }
      {!isTableView && <div className="w-3/5 mx-auto">
        <TimeLogList
          timeLogs={timeLogs}
          mode={mode}
          {...timeLogMutations}
        />
      </div>}
    </div>
  )
}