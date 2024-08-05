import * as React from 'react';
import dayjs from 'dayjs';
import {styled} from '@mui/material/styles';
import {PickersDay} from '@mui/x-date-pickers/PickersDay';
import {DatePicker} from "@mui/x-date-pickers";
import updateLocale from 'dayjs/plugin/updateLocale'
import ArrowBackIosIcon from "@mui/icons-material/ArrowBackIos";
import Button from "@mui/material/Button";
import ArrowForwardIosIcon from "@mui/icons-material/ArrowForwardIos.js";

dayjs.extend(updateLocale)
dayjs.updateLocale("en", {
  weekStart: 1
})
const CustomPickersDay = styled(PickersDay, {
  shouldForwardProp: (prop) => prop !== "isSelected" && prop !== "isHovered",
})(({theme, isSelected, isHovered, day}) => ({
  borderRadius: 0,
  ...(isSelected && {
    backgroundColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    "&:hover, &:focus": {
      backgroundColor: theme.palette.primary.main,
    },
  }),
  ...(isHovered && {
    backgroundColor: theme.palette.primary[theme.palette.mode],
    "&:hover, &:focus": {
      backgroundColor: theme.palette.primary[theme.palette.mode],
    },
  }),
  ...(day.day() === 1 && {
    borderTopLeftRadius: "50%",
    borderBottomLeftRadius: "50%",
  }),
  ...(day.day() === 0 && {
    borderTopRightRadius: "50%",
    borderBottomRightRadius: "50%",
  }),
}));

const isInSameWeek = (dayA, dayB) => {
  if (dayB == null) {
    return false;
  }

  return dayA.isSame(dayB, "week");
};

function Day({day, selectedDay, hoveredDay, ...other}) {
  return (
    <CustomPickersDay
      {...other}
      day={day}
      sx={{px: 2.5}}
      disableMargin
      selected={false}
      isSelected={isInSameWeek(day, selectedDay)}
      isHovered={isInSameWeek(day, hoveredDay)}
    />
  );
}

export default function WeekPicker({date, setDate, isPlaceholderData}) {
  const [hoveredDay, setHoveredDay] = React.useState(null);

  const startOfWeekDate = date.startOf('week')
  const endOfWeekDate = date.endOf('week')

  return (
    <div className="flex items-center justify-center">
      <Button
        onClick={() => {
          setDate(date.subtract(1, "week"))
        }}
        disabled={isPlaceholderData}
      >
        <ArrowBackIosIcon />
      </Button>
      <DatePicker
        className="w-44"
        label={`${startOfWeekDate.format("DD/MM/YYYY")} - ${endOfWeekDate.format("DD/MM/YYYY")}`}
        value={date.startOf("week")}
        onChange={(newValue) => setDate(newValue)}
        showDaysOutsideCurrentMonth
        slots={{day: Day}}
        slotProps={{
          day: (ownerState) => ({
            selectedDay: date,
            hoveredDay,
            onPointerEnter: () => setHoveredDay(ownerState.day),
            onPointerLeave: () => setHoveredDay(null),
          }),
          textField: {size: "small"}
        }}
        format="DD/MM/YYYY"
      />
      <Button
        onClick={() => {
          setDate(date.add(1, "week"))
        }}
        disabled={isPlaceholderData}
      >
        <ArrowForwardIosIcon />
      </Button>
    </div>
  );
}
