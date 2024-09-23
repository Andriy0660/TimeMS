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
import {CircularProgress} from "@mui/material";

export default function MonthPage() {
  const offset = startHourOfDay;
  const [calendarApi, setCalendarApi] = useState(null);

  const {date, setDate, addAlert} = useAppContext();
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

  const getDayCellClassNames = ({dow: dayOfWeek, date}) => {
    const dayInfo = data.items?.find(dayInfo => dayjs(dayInfo.date).isSame(dayjs(date), "day"));
    const {synced, conflicted} = dayInfo || {};
    if (synced || conflicted) {
      return ["bg-red-200 hover:cursor-pointer hover:bg-red-300"];
    } else if (dayOfWeek === 0 || dayOfWeek === 6) {
      return ["bg-red-50 hover:bg-red-100 hover:cursor-pointer"];
    } else {
      return ["bg-transparent hover:bg-blue-100 hover:cursor-pointer"];
    }
  }

  const getCellContent = ({dayNumberText, date}) => {
    const dayInfo = data.items?.find(dayInfo => dayjs(dayInfo.date).isSame(dayjs(date), "day"));
    return (
      <div className="flex justify-between p-1">
        {dayInfo &&
          <div>
            <StatusIcon isSynced={dayInfo.synced} isConflicted={dayInfo.conflicted} />
          </div>
        }
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

  return (
    <div className="mt-6 w-2/3 mx-auto">
      <div className="flex items-center">
        <div className="w-1/3 font-medium">
          Month: {data.totalHours}
        </div>
      </div>
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
    </div>
  );
}
