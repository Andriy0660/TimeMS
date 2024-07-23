import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import Button from "@mui/material/Button";
import {MobileDatePicker} from "@mui/x-date-pickers";

export default function DayPicker({date, setDate}) {
  return (
    <div className="flex items-center justify-center">
      <Button onClick={() => {
        setDate(date.subtract(1, "day"))
      }}>
        <ArrowBackIosIcon />
      </Button>
      <MobileDatePicker
        size="small"
        slotProps={{textField: {size: "small"}}}
        className="w-32"
        value={date}
        onChange={(newValue) => setDate(newValue)}
      />
      <Button onClick={() => {
        setDate(date.add(1, "day"))
      }}>
        <ArrowForwardIosIcon />
      </Button>
    </div>
  );
}