import {useEffect, useState} from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import dayjs from "dayjs";
import WeekPicker from "../components/WeekPicker.jsx";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {LocalizationProvider} from "@mui/x-date-pickers";
import dateTimeService from "../service/dateTimeService.js";
import {startHourOfDay} from "../config/timeConfig.js";
import {useQuery} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {CircularProgress} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import {useNavigate} from "react-router-dom";


export default function WeekPage() {
  const queryParams = new URLSearchParams(location.search);
  const [date, setDate] = useState(queryParams.get("date") ? dayjs(queryParams.get("date")) : dayjs());
  const offset = startHourOfDay;

  const {addAlert} = useAppContext();
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams();
    if (date && !dayjs().isSame(date, "day")) {
      params.set("date", dateTimeService.getFormattedDateTime(date));
    }
    navigate({search: params.toString()});
  }, [date]);

  const {
    data,
    isPending,
    isPlaceholderData,
  } = useQuery({
    queryKey: [timeLogApi.key, date, offset],
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
    navigate(`/app/timelog?date=${date}`)
  }

  if (isPending) {
    return (
      <div className="absolute inset-1/2">
        <CircularProgress />
      </div>
    );
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <div>
        <WeekPicker className="mt-4" date={date} setDate={setDate} isPlaceholderData={isPlaceholderData}/>
        <TableContainer className="flex mx-auto my-6 w-2/3" component={Paper}>
          <Table size="small" aria-label="a dense table">
            <TableHead>
              <TableRow>
                {data.map(dayInfo => <TableCell
                  key={dayInfo.date}
                  onClick={() => handleClick(dayInfo.date)}
                  className="hover:bg-blue-50 cursor-pointer"
                >
                  {dayInfo.dayName}
                </TableCell>)}
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                {data.map(dayInfo => <TableCell key={dayInfo.date}>{dayInfo.duration}</TableCell>)}
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      </div>
    </LocalizationProvider>
  )
}