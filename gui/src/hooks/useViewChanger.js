import {useNavigate} from "react-router-dom";
import useAppContext from "../context/useAppContext.js";

export default function useViewChanger() {
  const navigate = useNavigate();
  const {view, setView} = useAppContext();

  const changeView = (newView) => {
    if(view === newView) return;
    setView(newView);
    let viewUrl;
    switch (newView) {
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
        console.error("No such view:", newView);
        return null;
    }
    const params = new URLSearchParams(location.search);
    params.set("view", newView);
    navigate({pathname: viewUrl, search: params.toString()});
  }
  return {changeView}
}