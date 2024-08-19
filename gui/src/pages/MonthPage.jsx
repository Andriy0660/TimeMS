import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from "@fullcalendar/interaction"
import {useEffect, useRef, useState} from "react";
import MonthPicker from "../components/MonthPicker..jsx";
import dayjs from "dayjs";
import useAppContext from "../context/useAppContext.js";
import dateTimeService from "../service/dateTimeService.js";
import {useNavigate} from "react-router-dom";

const INITIAL_EVENTS = [
  {
    id: 1,
    title: '5h 24m',
    start: "2024-08-15T12:00:00"
  },
  {
    id: 2,
    title: '1h 5m',
    start: "2024-08-16T12:00:00"
  }
]

export default function MonthPage() {
  const calendarRef = useRef(null)
  const [calendarApi, setCalendarApi] = useState(null);

  const {date, setDate} = useAppContext();
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams();
    if (date && !dayjs().isSame(date, "day")) {
      params.set("date", dateTimeService.getFormattedDateTime(date));
    }
    navigate({search: params.toString()});
  }, [date]);

  useEffect(() => {
    if (calendarRef.current) {
      const api = calendarRef.current.getApi();
      api.gotoDate(new Date(date))
      setCalendarApi(api);
    }
  }, [calendarRef.current]);

  const handleClick = (date) => {
    setDate(dayjs(date))
    navigate(`/app/timelog`)
  }

  function renderEventContent(eventInfo) {
    return (
      <div
        onClick={() => handleClick(eventInfo.event.start)}
        className= "w-full mx-auto bg-white text-black text-lg font-medium hover:bg-blue-50 hover:cursor-pointer"
      >
        {eventInfo.event.title}
      </div>
    )
  }

  return (
    <div className="w-2/3 mx-auto">
      <MonthPicker
        date={date}
        toNext={() => {
          calendarApi?.next();
          setDate(date.add(1, "month"));
        }}
        toPrev={() => {
          calendarApi?.prev();
          setDate(date.subtract(1, "month"));
        }}
        classNames="my-2"
      />
      <FullCalendar
        initialEvents={INITIAL_EVENTS}
        ref={calendarRef}
        plugins={[dayGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        fixedWeekCount={false}
        firstDay={1}
        headerToolbar={null}
        aspectRatio={1.75}
        dayCellClassNames={({dow: dayOfWeek}) => {
          if (dayOfWeek === 0 || dayOfWeek === 6) {
            return ["bg-red-50"]
          } else {
            return ["bg-white"]
          }
        }}
        eventContent={renderEventContent}
        eventClassNames={() => {
          return ["bg-white"]
        }}
      />
    </div>
  )
}