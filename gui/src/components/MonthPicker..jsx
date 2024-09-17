import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import Button from "@mui/material/Button";
import {MobileDatePicker} from "@mui/x-date-pickers";
import useAppContext from "../context/useAppContext.js";

export default function MonthPicker({isOnNavBar, isLoading, classNames}) {
  const {date, setDate} = useAppContext()
  return (
    <div className={`flex items-center justify-center ${classNames}`}>
      <Button
        className={`${isOnNavBar ? "text-white" : ""}`}
        onClick={() => setDate(date.subtract(1, "month"))}
        disabled={isLoading}
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
        className={`${isOnNavBar ? "text-white" : ""}`}
        onClick={() => setDate(date.add(1, "month"))}
        disabled={isLoading}
      >
        <ArrowForwardIosIcon />
      </Button>
    </div>
  );
}