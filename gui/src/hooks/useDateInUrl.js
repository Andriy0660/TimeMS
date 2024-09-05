import {useEffect} from "react";
import {useNavigate} from "react-router-dom";
import dayjs from "dayjs";
import dateTimeService from "../service/dateTimeService.js";

export default function useDateInUrl(date) {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (date && !dayjs().isSame(date, "day")) {
      params.set("date", dateTimeService.getFormattedDateTime(date));
    }
    navigate({search: params.toString()});
  }, [date, navigate]);
}
