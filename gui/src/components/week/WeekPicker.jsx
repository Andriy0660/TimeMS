import * as React from 'react';
import dayjs from 'dayjs';
import {styled} from '@mui/material/styles';
import {PickersDay} from '@mui/x-date-pickers/PickersDay';
import {DatePicker} from "@mui/x-date-pickers";
import updateLocale from 'dayjs/plugin/updateLocale'
import ArrowBackIosIcon from "@mui/icons-material/ArrowBackIos";
import Button from "@mui/material/Button";
import ArrowForwardIosIcon from "@mui/icons-material/ArrowForwardIos.js";
import useAppContext from "../../context/useAppContext.js";

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

export default function WeekPicker({buttonColor}) {
  const {date, setDate} = useAppContext()
  const [hoveredDay, setHoveredDay] = React.useState(null);

  const startOfWeek = date.startOf('week').format('DD/MM/YYYY');
  const endOfWeek = date.endOf('week').format('DD/MM/YYYY');
  const weekRange = `${startOfWeek} - ${endOfWeek}`;


  return (
    <div className={`flex items-center justify-center`}>
      <Button
        className={`text-${buttonColor}`}
        onClick={() => setDate(date.subtract(1, "week"))}
      >
        <ArrowBackIosIcon />
      </Button>
      <DatePicker
        className="bg-white rounded w-[260px]"
        onChange={(newValue) => setDate(newValue)}
        value={date}
        showDaysOutsideCurrentMonth
        slots={{day: Day}}
        slotProps={{
          day: (ownerState) => ({
            selectedDay: date,
            hoveredDay,
            onPointerEnter: () => setHoveredDay(ownerState.day),
            onPointerLeave: () => setHoveredDay(null),
          }),
          textField: {
            size: "small",
            inputProps: {
              value: weekRange,
            },
          }
        }}
      />
      <Button
        className={`text-${buttonColor}`}
        onClick={() => setDate(date.add(1, "week"))}
      >
        <ArrowForwardIosIcon />
      </Button>
    </div>
  );
}
