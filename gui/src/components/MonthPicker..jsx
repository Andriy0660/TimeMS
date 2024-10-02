import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import Button from "@mui/material/Button";
import {MobileDatePicker} from "@mui/x-date-pickers";
import useAppContext from "../context/useAppContext.js";

export default function MonthPicker({buttonColor, classNames}) {
  const {date, setDate} = useAppContext()
  return (
    <div className={`flex items-center justify-center ${classNames}`}>
      <Button
        className={`text-${buttonColor}`}
        onClick={() => setDate(date.subtract(1, "month"))}
      >
        <ArrowBackIosIcon />
      </Button>
      <MobileDatePicker
        views={["month", "year"]}
        slotProps={{textField: {size: "small"}}}
        className="w-40 bg-white rounded"
        value={date}
        onChange={(newValue) => setDate(newValue)}
      />
      <Button
        className={`text-${buttonColor}`}
        onClick={() => setDate(date.add(1, "month"))}
      >
        <ArrowForwardIosIcon />
      </Button>
    </div>
  );
}