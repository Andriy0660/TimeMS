import VerticalDivider from "./VerticalDivider.jsx";
import {Icon, Tooltip, Typography} from "@mui/material";
import dateTimeService from "../../service/dateTimeService.js";
import {TiArrowForward} from "react-icons/ti";
import {isJiraSyncingEnabled} from "../../config/config.js";

export default function TimeLogNonEditableFields({
  startTime,
  endTime,
  ticket,
  isTimeLogInNextDay,
  setIsEditing,
  setEditedField
}) {

  const handleTimeClick = (field) => () => {
    setIsEditing?.(true);
    setEditedField?.(field);
  };

  return (
    <>
      {(startTime || endTime) && (
        <TimeDisplay
          time={startTime}
          isNextDay={isTimeLogInNextDay.startTime}
          onClick={handleTimeClick("startTime")}
          className="mr-4"
        />
      )}

      {endTime && (
        <>
          -
          <TimeDisplay
            time={endTime}
            isNextDay={isTimeLogInNextDay.endTime > 0}
            onClick={handleTimeClick("endTime")}
            className="mx-4"
          />
        </>
      )}

      {isJiraSyncingEnabled && ticket && (
        <>
          {(startTime || endTime) && <VerticalDivider className="my-0.5 mr-4" />}
          <TicketDisplay
            ticket={ticket}
            onClick={handleTimeClick("ticket")}
          />
        </>
      )}
    </>
  )
}

const TimeDisplay = ({time, isNextDay, onClick, className}) => {
  if (!time && !placeholder) return null;
  const placeholder = "____";

  return (
    <div className={`flex hover:bg-blue-100 ${className}`} onClick={onClick}>
      {time && isNextDay && (
        <NextDayIndicator />
      )}
      <Typography
        className={`text-sm ${time ? "font-bold" : "text-xs leading-6"}`}
      >
        {time ? dateTimeService.getFormattedTime(time) : placeholder}
      </Typography>
    </div>
  );
};

const NextDayIndicator = () => (
  <Tooltip className="flex items-center mr-1" title="Next day">
    <Icon fontSize="small">
      <TiArrowForward />
    </Icon>
  </Tooltip>
);

const TicketDisplay = ({ticket, onClick}) => {
  if (!ticket) return null;

  return (
    <div className="w-fit text-nowrap mr-4 hover:bg-blue-100" onClick={onClick}>
      <Typography className="font-bold text-sm">{ticket}</Typography>
    </div>
  );
};