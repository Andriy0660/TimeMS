import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from "@fullcalendar/interaction"
import {useEffect, useRef, useState} from "react";
import MonthPicker from "../components/MonthPicker..jsx";
import dayjs from "dayjs";
import useAppContext from "../context/useAppContext.js";
import dateTimeService from "../service/dateTimeService.js";
import {useQuery} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {startHourOfDay} from "../config/timeConfig.js";
import {CircularProgress} from "@mui/material";
import useDateInUrl from "../hooks/useDateInUrl.js";
import {useNavigate} from "react-router-dom";

export default function MonthPage() {
  const offset = startHourOfDay;
  const calendarRef = useRef(null);
  const [calendarApi, setCalendarApi] = useState(null);
  const [calendarKey, setCalendarKey] = useState(Date.now());

  const {date, setDate, addAlert} = useAppContext();
  const navigate = useNavigate();
  useDateInUrl(date);

  useEffect(() => {
    if (calendarRef.current) {
      const api = calendarRef.current.getApi();
      setCalendarApi(api);
    }
  }, [calendarRef.current]);

  const {data, isPending, isPlaceholderData} = useQuery({
    queryKey: [timeLogApi.key, date, offset],
    queryFn: () => timeLogApi.getHoursForMonth({date: dateTimeService.getFormattedDate(date), offset}),
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Getting hours for month failed:", error);
    },
    placeholderData: (prev) => prev,
    retryDelay: 300,
  });

  useEffect(() => {
    if (calendarApi && data) {
      calendarApi.removeAllEvents();
      calendarApi.addEventSource(data);
      setCalendarKey(Date.now());
    }
  }, [data]);

  const handleClick = (date) => {
    setDate(dayjs(date));
    navigate(`/app/timelog`);
  };

  function renderEventContent(eventInfo) {
    return (
      <div
        onClick={() => handleClick(eventInfo.event.start)}
        className="flex justify-center w-full bg-transparent text-black text-lg font-medium hover:bg-blue-50 hover:cursor-pointer"
      >
        {eventInfo.event.title !== "0h 0m" ? eventInfo.event.title : ""}
      </div>
    );
  }

  if (isPending) {
    return (
      <div className="absolute inset-1/2">
        <CircularProgress />
      </div>
    );
  }

  return (
    <div className="w-2/3 mx-auto">
      <MonthPicker
        date={date}
        toNext={() => {
          setDate(date.add(1, "month"));
          calendarApi?.next();
        }}
        toPrev={() => {
          setDate(date.subtract(1, "month"));
          calendarApi?.prev();
        }}
        classNames="my-2"
        isLoading={isPlaceholderData}
      />
      <FullCalendar
        key={calendarKey}
        initialDate={new Date(date)}
        events={data}
        ref={calendarRef}
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
        eventContent={renderEventContent}
        eventClassNames={() => {
          return ["bg-transparent"];
        }}
      />
    </div>
  );
}
