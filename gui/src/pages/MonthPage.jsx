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

export default function MonthPage() {
  const offset = startHourOfDay;
  const [calendarApi, setCalendarApi] = useState(null);

  const {date, setDate, addAlert} = useAppContext();
  const {changeView} = useViewChanger();

  const {data} = useQuery({
    queryKey: [timeLogApi.key, "month", date, offset],
    queryFn: () => timeLogApi.getHoursForMonth({date: dateTimeService.getFormattedDate(date), offset}),
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Getting hours for month failed:", error);
    },
    initialData: () => [],
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

  const getEventContent = (eventInfo) => {
    const {start, title} = eventInfo.event;
    const {synchronized, conflicted, inProgress} = eventInfo.event.extendedProps;
    return <MonthPageDuration isSynchronized={synchronized} isConflicted={conflicted} isInProgress={inProgress} title={title} handleClickDate={() => handleClickDate(start)} />
  }

  const getEventClassNames = (eventInfo) => {
    const {synchronized, conflicted, inProgress} = eventInfo.event.extendedProps;

    if (synchronized || conflicted) {
      return ["bg-red-200 hover:bg-transparent"];
    } else if (inProgress) {
      return ["bg-blue-200 hover:bg-transparent"];
    } else {
      return ["bg-transparent"];
    }
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
        events={data.items}
        ref={handleCalendarRef}
        plugins={[dayGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        fixedWeekCount={false}
        firstDay={1}
        headerToolbar={null}
        aspectRatio={1.75}
        dayCellClassNames={({dow: dayOfWeek}) => {
          if (dayOfWeek === 0 || dayOfWeek === 6) {
            return ["bg-red-50"];
          } else {
            return ["bg-white hover:bg-blue"];
          }
        }}
        eventContent={getEventContent}
        eventClassNames={getEventClassNames}
      />
    </div>
  );
}
