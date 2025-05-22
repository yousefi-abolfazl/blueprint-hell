package model;

import controller.Constants;
import java.awt.*;

public class SquarePort extends Port {
    
    public SquarePort(Point position, boolean isInput, NetworkSystem parentSystem) {
        super(position, Constants.PORT_SQUARE_SIZE, isInput, parentSystem);
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
        
        // پر کردن مربع
        g.fillRect(position.x - portSize / 2, position.y - portSize / 2, portSize, portSize);
        
        // تنظیم ضخامت خط برای حاشیه
        g.setStroke(new BasicStroke(1.0f));
        
        // رسم حاشیه با رنگ تیره‌تر
        g.setColor(Color.BLACK);
        g.drawRect(position.x - portSize / 2, position.y - portSize / 2, portSize, portSize);
        
        // بازگرداندن وضعیت اصلی گرافیک
        g.setStroke(originalStroke);
    }
} 