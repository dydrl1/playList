import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "@/components/pages/Home";
import Login from "@/components/pages/Login";
import Signup from "@/components/pages/signup";

export default function App() {
  return (
    <Router>
      <div className="min-h-screen bg-background text-foreground">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
        </Routes>
      </div>
    </Router>
  );


}
