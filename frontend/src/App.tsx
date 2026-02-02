import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "@/components/pages/Home";

export default function App() {
  return (
    <Router>
      <div className="min-h-screen bg-background text-foreground">
        <Routes>
          <Route path="/" element={<Home />} />
        </Routes>
      </div>
    </Router>
  );
}
