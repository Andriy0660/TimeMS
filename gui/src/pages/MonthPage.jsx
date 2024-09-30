import "../styles/MonthPage.css"
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from "@fullcalendar/interaction"
import {useEffect, useState} from "react";
import dayjs from "dayjs";
import useAppContext from "../context/useAppContext.js";
import dateTimeService from "../service/dateTimeService.js";
import {useQuery} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {startHourOfDay} from "../config/timeConfig.js";
import MonthPageDuration from "../components/MonthPageDuration.jsx";
import useViewChanger from "../hooks/useViewChanger.js";
import StatusIcon from "../components/StatusIcon.jsx";
import {CircularProgress, FormControlLabel, IconButton, Switch, Tooltip} from "@mui/material";
import TimeLogList from "../components/TimeLogList.jsx";
import useTimeLogMutations from "../hooks/useTimeLogMutations.js";
import useProcessedTimeLogs from "../hooks/useProcessedTimeLogs.js";
import {GoTable} from "react-icons/go";
import ReorderIcon from "@mui/icons-material/Reorder.js";
import {monthViewMode} from "../consts/monthViewMode.js";

export default function MonthPage() {
  const offset = startHourOfDay;
  const [calendarApi, setCalendarApi] = useState(null);
  const [viewMode, setViewMode] = useState(monthViewMode.CALENDAR);
  const {date, setDate, addAlert, mode} = useAppContext();
  const {changeView} = useViewChanger();

  const {data, isPending} = useQuery({
    queryKey: [timeLogApi.key, "month", date, offset],
    queryFn: () => timeLogApi.getHoursForMonth({date: dateTimeService.getFormattedDate(date), offset}),
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Getting hours for month failed:", error);
    },
    retryDelay: 300,
  });

  const timeLogMutations = useTimeLogMutations();
  const {
    groupByDescription, setGroupByDescription, timeLogs
  } = useProcessedTimeLogs();

  useEffect(() => {
    if(calendarApi && calendarApi.currentData.currentDate.getMonth() !== date.get("month")) {
      queueMicrotask(() => {
        calendarApi.gotoDate(new Date(date))
      })
    }
  }, [calendarApi, date])

  const handleCalendarRef = (calendar) => {
    if (calendar) {
      setCalendarApi(calendar.getApi());
    }
  };

  const handleClickDate = (date) => {
    setDate(dayjs(date));
    changeView("Day")
  };

  const getDayCellClassNames = ({dow: dayOfWeek, date: cellDate}) => {
    const dayInfo = data.items?.find(dayInfo => dayjs(dayInfo.date).isSame(dayjs(cellDate), "day"));
    const {synced, conflicted} = dayInfo || {};
    if ((!synced || conflicted) && dayjs(cellDate).$M === date.$M) {
      return ["bg-red-200 hover:cursor-pointer hover:bg-red-300"];
    } else if (dayOfWeek === 0 || dayOfWeek === 6) {
      return ["bg-red-50 hover:bg-red-100 hover:cursor-pointer"];
    } else {
      return ["bg-transparent hover:bg-blue-100 hover:cursor-pointer"];
    }
  }

  const getCellContent = ({dayNumberText, date: cellDate}) => {
    const dayInfo = data.items?.find(dayInfo => dayjs(dayInfo.date).isSame(dayjs(cellDate), "day"));
    return (
      <div className="flex justify-between p-1">
          <div>
            {dayInfo && dayjs(cellDate).$M === date.$M && (
              <StatusIcon isSynced={dayInfo.synced} isConflicted={dayInfo.conflicted} />
            )}
          </div>
        <div>
          {dayNumberText}
        </div>
      </div>
    );
  }

  const getEventContent = (eventInfo) => {
    const {duration} = eventInfo.event.extendedProps;
    return <MonthPageDuration duration={duration} />
  }

  if(isPending) {
    return (
      <div className="absolute inset-1/2">
        <CircularProgress />
      </div>
    );
  }

  const activeIconClasses = "border-blue-400 border-solid border rounded";
  return (
    <div className="my-6 w-2/3 mx-auto">
      <div className="flex items-center mb-2">
        <Tooltip title="Calendar">
          <IconButton
            onClick={() => setViewMode(monthViewMode.CALENDAR)}
            className={`${viewMode === "Calendar" ? activeIconClasses : ""}`}><GoTable /></IconButton>
        </Tooltip>
        <Tooltip title="List">
          <IconButton
            onClick={() => setViewMode(monthViewMode.LIST)}
            className={`${viewMode === "List" ? activeIconClasses : ""}`}><ReorderIcon /></IconButton>
        </Tooltip>
        <div className="font-medium ml-10">
          Month: {data.totalHours}
        </div>
        {viewMode === "List" && <FormControlLabel
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
      {viewMode === monthViewMode.CALENDAR &&
        <FullCalendar
          initialDate={new Date(date)}
          events={data.items?.map(item => {
            item.extendedProps = {duration: item.duration}
            return item;
          })}
          ref={handleCalendarRef}
          plugins={[dayGridPlugin, interactionPlugin]}
          initialView="dayGridMonth"
          fixedWeekCount={false}
          firstDay={1}
          headerToolbar={null}
          aspectRatio={1.75}
          dayCellClassNames={getDayCellClassNames}
          dayCellDidMount={({el, date}) => {
            el.addEventListener("click", () => handleClickDate(date));
          }}
          dayCellContent={getCellContent}
          eventContent={getEventContent}
          eventClassNames={() => ["bg-transparent"]}
        />
      }
      {monthViewMode.LIST && <TimeLogList
        timeLogs={timeLogs}
        mode={mode}
        {...timeLogMutations}
      />}
    </div>
  );
}
