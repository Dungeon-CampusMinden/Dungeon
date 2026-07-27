export function CustomIcon({
  src,
  alt,
  className,
  size = 16,
}: {
  src: string;
  alt: string;
  className?: string;
  size?: number;
}) {
  return (
    <img src={src} alt={alt} className={`mt-0 ${className ?? ""}`} style={{ width: size, height: size }} />
  );
}
