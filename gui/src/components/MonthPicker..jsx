import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import Button from "@mui/material/Button";
import {MobileDatePicker} from "@mui/x-date-pickers";

export default function MonthPicker({date, toNext, toPrev, isLoading, classNames}) {
  return (
    <div className={`flex items-center justify-center ${classNames}`}>
      <Button
        onClick={toPrev}
        disabled={isLoading}
      >
        <ArrowBackIosIcon />
      </Button>
      <MobileDatePicker
        views={["month", "year"]}
        slotProps={{textField: {size: "small"}}}
        className="w-40"
        value={date}
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