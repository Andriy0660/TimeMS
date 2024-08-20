import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from "@fullcalendar/interaction"
import {useState} from "react";
import MonthPicker from "../components/MonthPicker..jsx";
import dayjs from "dayjs";
import useAppContext from "../context/useAppContext.js";
import dateTimeService from "../service/dateTimeService.js";
import {useQuery} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {startHourOfDay} from "../config/timeConfig.js";
import useDateInUrl from "../hooks/useDateInUrl.js";
import {useNavigate} from "react-router-dom";
import MonthPageDuration from "../components/MonthPageDuration.jsx";

export default function MonthPage() {
  const offset = startHourOfDay;
  const [calendarApi, setCalendarApi] = useState(null);

  const {date, setDate, addAlert} = useAppContext();
  const navigate = useNavigate();
  useDateInUrl(date);

  const {data} = useQuery({
    queryKey: [timeLogApi.key, date, offset],
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

  const handleCalendarRef = (calendar) => {
    if (calendar) {
      setCalendarApi(calendar.getApi());
    }
  };

  const handleClickDate = (date) => {
    setDate(dayjs(date));
    navigate(`/app/timelog`);
  };

  return (
    <div className="w-2/3 mx-auto">
      <div className="flex items-center">
        <div className="w-1/3 font-medium">
          Month: {data.totalHours}
        </div>
        <div className="min-w-1/3">
          <MonthPicker
            date={date}
            toNext={() => {
              setDate(date.add(1, "month"));
              calendarApi.next();
            }}
            toPrev={() => {
              setDate(date.subtract(1, "month"));
              calendarApi.prev();
            }}
            classNames="my-2"
          />
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
            return ["bg-white"];
          }
        }}
        eventContent={(eventInfo) =>
          <MonthPageDuration title={eventInfo.event.title} handleClickDate={() => handleClickDate(eventInfo.event.start)} />}
        eventClassNames={() => ["bg-transparent"]}
      />
    </div>
  );
}
