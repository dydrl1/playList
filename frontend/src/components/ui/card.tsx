import { FC, ReactNode } from "react";

interface CardProps {
  children: ReactNode;
  className?: string;
}

export const Card: FC<CardProps> = ({ children, className }) => {
  return (
    <div className={`bg-neutral-800 rounded-md shadow p-4 ${className}`}>
      {children}
    </div>
  );
};
