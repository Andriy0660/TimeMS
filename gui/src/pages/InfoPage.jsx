import {Typography} from "@mui/material";
import NavBar from "../components/NavBar.jsx";

export default function InfoPage() {

  return (
    <div>
      <div className="flex justify-center items-center h-screen">
        <Typography className="h-1/3 text-5xl">
          <div className="text-blue-700 shadow-2xl shadow-blue-200">The best choice for managing your time!</div>
        </Typography>
      </div>
    </div>

  );
}