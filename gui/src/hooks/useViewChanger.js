import {useNavigate} from "react-router-dom";
import useAppContext from "../context/useAppContext.js";
import {viewMode} from "../consts/viewMode.js";

export default function useViewChanger() {
  const navigate = useNavigate();
  const {mode, setMode} = useAppContext();

  const changeView = (newViewMode) => {
    if(mode === newViewMode) return;
    setMode(newViewMode);
    let viewUrl;
    switch (newViewMode) {
      case viewMode.DAY :
        viewUrl = "/app/timelog";
        break;
      case viewMode.WEEK :
        viewUrl = "/app/weekview";
        break;
      case viewMode.MONTH :
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