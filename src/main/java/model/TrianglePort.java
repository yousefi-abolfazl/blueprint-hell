package model;

import controller.Constants;
import java.awt.*;

public class TrianglePort extends Port {
    
    public TrianglePort(Point position, boolean isInput, NetworkSystem parentSystem) {
        super(position, Constants.PORT_TRIANGLE_SIZE, isInput, parentSystem);
    }
    
    @Override
    public void render(Graphics2D g) {
        int portSize = size * 10;
        
        // ذخیره‌سازی وضعیت فعلی گرافیک
        Stroke originalStroke = g.getStroke();
        
        // تنظیم رنگ پورت بر اساس نوع (ورودی/خروجی)
        if (isInput) {
            g.setColor(Constants.PORT_INPUT_COLOR);
        } else {
            g.setColor(Constants.PORT_OUTPUT_COLOR);
        }
        
        int[] xPoints = {
            position.x,
            position.x - portSize / 2,
            position.x + portSize / 2
        };
        
        int[] yPoints = {
            position.y - portSize / 2,
            position.y + portSize / 2,
            position.y + portSize / 2
        };
        
        // پر کردن مثلث
        g.fillPolygon(xPoints, yPoints, 3);
        
        // تنظیم ضخامت خط برای حاشیه
        g.setStroke(new BasicStroke(1.0f));
        
        // رسم حاشیه با رنگ تیره‌تر
        g.setColor(Color.BLACK);
        g.drawPolygon(xPoints, yPoints, 3);
        
        // بازگرداندن وضعیت اصلی گرافیک
        g.setStroke(originalStroke);
    }
} 