package model;

import controller.Constants;
import java.awt.*;

public class TrianglePacket extends Packet {
    
    public TrianglePacket(Point position) {
        super(position, Constants.PACKET_TRIANGLE_SIZE, 2); // Size 3, coin value 2
        this.maxSpeed = Constants.PACKET_TRIANGLE_SPEED;
        this.currentSpeed = 0.8; // Start with some initial speed
        this.acceleration = 0.2;
        this.deceleration = 0.1;
    }
    
    @Override
    public void render(Graphics2D g) {
        // Calculate packet visual size (make it smaller)
        int visualSize = this.size * 6; // Reduced from 10 to 6
        
        // اینجا وقتی مثلث را رندر می‌کنیم به جهت حرکت آن توجه کنیم
        int[] xPoints;
        int[] yPoints;
        
        if (isMoving && targetPosition != null) {
            // محاسبه جهت حرکت
            double dx = targetPosition.x - position.x;
            double dy = targetPosition.y - position.y;
            double angle = Math.atan2(dy, dx);
            
            // محاسبه نقاط مثلث با توجه به جهت حرکت
            xPoints = new int[]{
                (int)(position.x + Math.cos(angle) * visualSize/2),
                (int)(position.x + Math.cos(angle + Math.PI*2/3) * visualSize/2),
                (int)(position.x + Math.cos(angle + Math.PI*4/3) * visualSize/2)
            };
            
            yPoints = new int[]{
                (int)(position.y + Math.sin(angle) * visualSize/2),
                (int)(position.y + Math.sin(angle + Math.PI*2/3) * visualSize/2),
                (int)(position.y + Math.sin(angle + Math.PI*4/3) * visualSize/2)
            };
        } else {
            // اگر در حال حرکت نیست، به سمت راست اشاره می‌کند
            xPoints = new int[]{position.x + visualSize/2, position.x - visualSize/2, position.x - visualSize/2};
            yPoints = new int[]{position.y, position.y - visualSize/2, position.y + visualSize/2};
        }

        // Draw main packet with solid fill, no glow effects
        g.setColor(Constants.PACKET_TRIANGLE_COLOR);
        g.fillPolygon(xPoints, yPoints, 3);
        
        // Draw outline 
        g.setStroke(new BasicStroke(Constants.PACKET_TRIANGLE_THICKNESS));
        g.setColor(Color.BLACK);
        g.drawPolygon(xPoints, yPoints, 3);
        
        // Draw noise indicator if noise > 0
        if (noise > 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.drawString(String.valueOf(noise), position.x - 3, position.y - 6);
        }
        
        // Debug info: draw position coordinates
        if (Constants.DEBUG_PACKET_MOVEMENT) {
            System.out.println("Rendering triangle packet at: " + position.x + "," + position.y);
        }
    }
    
    @Override
    public boolean isCompatibleWithPort(Port port) {
        return port instanceof TrianglePort;
    }
    
    @Override
    public void startMoving(Point target, Port sourcePort) {
        super.startMoving(target, sourcePort);
        
        // تنظیم دقیق سرعت و شتاب برای پکت مثلثی
        if (sourcePort != null) {
            if (isCompatibleWithPort(sourcePort)) {
                // سرعت ثابت برای پورت سازگار
                this.currentSpeed = this.maxSpeed;
                this.isAccelerating = false;
            } else {
                // شتاب بیشتر برای پورت ناسازگار
                this.currentSpeed = 0.5; // سرعت اولیه کمتر
                this.isAccelerating = true;
                this.acceleration = 0.35; // شتاب بیشتر
            }
        } else {
            // حالت پیش‌فرض
            this.currentSpeed = 1.0;
            this.isAccelerating = true;
        }
        
        // اطمینان از سرعت مناسب
        if (this.currentSpeed <= 0) {
            this.currentSpeed = 0.5;
        }
        
        System.out.println("Triangle packet starting to move with speed: " + this.currentSpeed + ", accelerating: " + this.isAccelerating);
    }
    
    @Override
    public void startMoving(Point target) {
        super.startMoving(target);
        
        // تنظیم دقیق سرعت و شتاب برای پکت مثلثی با استفاده از sourcePort ذخیره شده
        Port srcPort = getSourcePort();
        if (srcPort != null) {
            if (isCompatibleWithPort(srcPort)) {
                // سرعت ثابت برای پورت سازگار
                this.currentSpeed = this.maxSpeed;
                this.isAccelerating = false;
            } else {
                // شتاب بیشتر برای پورت ناسازگار
                this.currentSpeed = 0.5; // سرعت اولیه کمتر
                this.isAccelerating = true;
                this.acceleration = 0.35; // شتاب بیشتر
            }
        } else {
            // حالت پیش‌فرض
            this.currentSpeed = 1.0;
            this.isAccelerating = true;
        }
        
        // اطمینان از سرعت مناسب
        if (this.currentSpeed <= 0) {
            this.currentSpeed = 0.5;
        }
        
        System.out.println("Triangle packet starting to move (overloaded) with speed: " + this.currentSpeed + ", accelerating: " + this.isAccelerating);
    }
    
    @Override
    public void update() {
        if (!isMoving) return;
        
        // بررسی مسیر فعلی
        if (targetPosition != null) {
            // محاسبه فاصله تا هدف
            double distance = Math.sqrt(
                Math.pow(targetPosition.x - position.x, 2) +
                Math.pow(targetPosition.y - position.y, 2)
            );
            
            // اگر فاصله زیاد است و سرعت پایین، افزایش سرعت
            if (distance > 100 && currentSpeed < 1.0) {
                currentSpeed = Math.min(maxSpeed, currentSpeed + 0.1);
            }
        }
        
        // فراخوانی update پایه
        super.update();
        
        // اطمینان از اینکه پکت متوقف نشده
        if (isMoving && currentSpeed < 0.5) {
            currentSpeed = 0.5;
        }
    }
} 