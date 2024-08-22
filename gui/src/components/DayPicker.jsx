import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import Button from "@mui/material/Button";
import {MobileDatePicker} from "@mui/x-date-pickers";
import useAppContext from "../context/useAppContext.js";

export default function DayPicker({date, toNext, toPrev, isLoading}) {
  const {setDate} = useAppContext();
  return (
    <div className="flex items-center justify-center">
      <Button
        onClick={toPrev}
        disabled={isLoading}
      >
        <ArrowBackIosIcon />
      </Button>
      <MobileDatePicker
        slotProps={{textField: {size: "small"}}}
        className="w-32"
        value={date}
        format="DD/MM/YYYY"
        onChange={(newValue) => setDate(newValue)}
      />
      <Button
        onClick={toNext}
        disabled={isLoading}
      >
        <ArrowForwardIosIcon />
      </Button>
    </div>
  );
}