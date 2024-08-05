import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import Button from "@mui/material/Button";
import {MobileDatePicker} from "@mui/x-date-pickers";

export default function MonthPicker({date, setDate, isPlaceholderData}) {
  return (
    <div className="flex items-center justify-center">
      <Button
        onClick={() => {
          setDate(date.subtract(1, "month"))
        }}
        disabled={isPlaceholderData}
      >
        <ArrowBackIosIcon />
      </Button>
      <MobileDatePicker
        views={["month", "year"]}
        slotProps={{textField: {size: "small"}}}
        className="w-40"
        value={date}
        onChange={(newValue) => setDate(newValue)}
      />
      <Button
        onClick={() => {
          setDate(date.add(1, "month"))
        }}
        disabled={isPlaceholderData}
      >
        <ArrowForwardIosIcon />
      </Button>
    </div>
  );
}