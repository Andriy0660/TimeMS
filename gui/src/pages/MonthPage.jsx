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
import {isJiraSyncingEnabled, isExternalServiceSyncingEnabled, startHourOfDay, externalTimeLogTimeCf} from "../config/config.js";
import MonthPageDuration from "../components/month/MonthPageDuration.jsx";
import useViewChanger from "../hooks/useViewChanger.js";
import TimeLogStatusIcons from "../components/timeLog/TimeLogStatusIcons.jsx";
import {FormControlLabel, Switch} from "@mui/material";
import TimeLogList from "../components/timeLog/TimeLogList.jsx";
import useTimeLogMutations from "../hooks/useTimeLogMutations.js";
import useProcessedTimeLogs from "../hooks/useProcessedTimeLogs.js";
import {GoTable} from "react-icons/go";
import ReorderIcon from "@mui/icons-material/Reorder.js";
import {monthViewMode} from "../consts/monthViewMode.js";
import ViewModeIcon from "../components/general/ViewModeIcon.jsx";
import {viewMode} from "../consts/viewMode.js";
import {syncStatus} from "../consts/syncStatus.js";
import LoadingPage from "../components/general/LoadingPage.jsx";
import useJiraSync from "../hooks/useJiraSync.js";
import BigLabel from "../components/general/BigLabel.jsx";
import SyncExternalTimeLogDuration from "../components/sync/SyncExternalTimeLogDuration.jsx";

export default function MonthPage() {
  const offset = startHourOfDay;
  const [calendarApi, setCalendarApi] = useState(null);
  const [view, setView] = useState(monthViewMode.CALENDAR);
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
  const syncMutations = useJiraSync();

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
    changeView(viewMode.DAY)
  };

  const getDayCellClassNames = ({dow: dayOfWeek, date: cellDate}) => {
    const cellDayjs = dayjs(cellDate);
    const dayInfo = data.items?.find(item => dayjs(item.date).isSame(cellDayjs, "day"));
    const isCurrentMonth = cellDayjs.isSame(date, 'month');

    const conflictedClassNames = ["bg-red-200 hover:cursor-pointer hover:bg-red-300"];

    const getSyncClassNames = (status) => {
      if (!status) return null;

      const notSyncedClassNames = ["bg-red-200 hover:cursor-pointer hover:bg-red-300"];
      const partiallySyncedClassNames = ["bg-orange-200 hover:cursor-pointer hover:bg-orange-300"];

      switch (status) {
        case syncStatus.NOT_SYNCED:
          return notSyncedClassNames;
        case syncStatus.PARTIAL_SYNCED:
          return partiallySyncedClassNames;
        default:
          return null;
      }
    };

    const getDefaultClassNames = (dayOfWeek) => {
      return dayOfWeek === 0 || dayOfWeek === 6
        ? ["bg-red-50 hover:bg-red-100 hover:cursor-pointer"]
        : ["bg-transparent hover:bg-blue-100 hover:cursor-pointer"];
    };

    if (isCurrentMonth) {
      if (dayInfo?.conflicted) {
        return conflictedClassNames;
      }

      if (isJiraSyncingEnabled) {
        const syncClassNames = getSyncClassNames(dayInfo.jiraSyncInfo.status);
        if (syncClassNames) return syncClassNames;
      }
      if (isExternalServiceSyncingEnabled) {
        const syncClassNames = getSyncClassNames(dayInfo.externalTimeLogSyncInfo.status);
        if (syncClassNames) return syncClassNames;
      }
    }

    return getDefaultClassNames(dayOfWeek);
  };

  const getCellContent = ({dayNumberText, date: cellDate}) => {
    const dayInfo = data.items?.find(dayInfo => dayjs(dayInfo.date).isSame(dayjs(cellDate), "day"));
    return (
      <div className="flex justify-between p-1">
          <div>
            {dayInfo && dayjs(cellDate).$M === date.$M && (
              <TimeLogStatusIcons isConflicted={dayInfo.conflicted} jiraSyncStatus={dayInfo.jiraSyncInfo.status} externalTimeLogSyncStatus={dayInfo.externalTimeLogSyncInfo.status} showOnlyNotSuccessfullySynced={true}/>
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
    const externalTimeLogDuration = dateTimeService.formatMinutesToHM(Math.round(
      dateTimeService.getMinutesFromHMFormat(duration) / externalTimeLogTimeCf));

    return (
      <>
        <MonthPageDuration duration={duration} />
        {isExternalServiceSyncingEnabled && <SyncExternalTimeLogDuration duration={externalTimeLogDuration} textSize="base"/>}
      </>
    )
  }

  if(isPending) {
    return <LoadingPage />
  }

  return (
    <div className="my-6 w-2/3 mx-auto">
      <div className="flex items-center mb-2">
        <ViewModeIcon
          title={monthViewMode.CALENDAR}
          icon={<GoTable />}
          isActive={view === monthViewMode.CALENDAR}
          onClick={() => setView(monthViewMode.CALENDAR)}
        />
        <ViewModeIcon
          title={monthViewMode.LIST}
          icon={<ReorderIcon />}
          isActive={view === monthViewMode.LIST}
          onClick={() => setView(monthViewMode.LIST)}
        />
        <BigLabel className="ml-12">{data.totalHours}</BigLabel>
        {view === monthViewMode.LIST && <FormControlLabel
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
      {view === monthViewMode.CALENDAR &&
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
      {view === monthViewMode.LIST && <TimeLogList
        timeLogs={timeLogs}
        mode={mode}
        {...timeLogMutations}
        {...syncMutations}
      />}
    </div>
  );
}
