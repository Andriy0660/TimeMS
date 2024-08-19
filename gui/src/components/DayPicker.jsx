import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import Button from "@mui/material/Button";
import {MobileDatePicker} from "@mui/x-date-pickers";

export default function DayPicker({date, toNext, toPrev, isLoading}) {
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