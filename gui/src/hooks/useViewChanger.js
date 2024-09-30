import {useNavigate} from "react-router-dom";
import useAppContext from "../context/useAppContext.js";

export default function useViewChanger() {
  const navigate = useNavigate();
  const {mode, setMode} = useAppContext();

  const changeView = (newViewMode) => {
    if(mode === newViewMode) return;
    setMode(newViewMode);
    let viewUrl;
    switch (newViewMode) {
      case "Day" :
        viewUrl = "/app/timelog";
        break;
      case "Week" :
        viewUrl = "/app/weekview";
        break;
      case "Month" :
        viewUrl = "/app/monthview";
        break;
      default :
        console.error("No such view:", newViewMode);
        return null;
    }
    const params = new URLSearchParams(location.search);
    params.set("mode", newViewMode);
    navigate({pathname: viewUrl, search: params.toString()});
  }
  return {changeView}
}