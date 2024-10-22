import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import Button from "@mui/material/Button";
import {MobileDatePicker} from "@mui/x-date-pickers";
import useAppContext from "../../context/useAppContext.js";

export default function DayPicker({buttonColor}) {
  const {date, setDate} = useAppContext()
  return (
    <div className="flex items-center justify-center ">
      <Button
        className={`text-${buttonColor}`}
        onClick={() => setDate(date.subtract(1, "day"))}
      >
        <ArrowBackIosIcon />
      </Button>
      <MobileDatePicker
        slotProps={{textField: {size: "small"}}}
        className="w-32 bg-white rounded"
        value={date}
        format="DD/MM/YYYY"
        onChange={(newValue) => setDate(newValue)}
      />
      <Button
        className={`text-${buttonColor}`}
        onClick={() => setDate(date.add(1, "day"))}
      >
        <ArrowForwardIosIcon />
      </Button>
    </div>
  );
}